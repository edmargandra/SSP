package org.jasig.ssp.web.api;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.validation.Valid;

import org.jasig.ssp.factory.TaskTOFactory;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.Task;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.SecurityService;
import org.jasig.ssp.service.TaskService;
import org.jasig.ssp.transferobject.TaskTO;
import org.jasig.ssp.transferobject.form.EmailPersonTasksForm;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

/**
 * Services to manipulate Tasks.
 * <p>
 * Mapped to URI path <code>/1/person/{personId}/task</code>
 */
@PreAuthorize("hasRole('ROLE_USER')")
@Controller
@RequestMapping("/1/person/{personId}/task")
public class PersonTaskController extends
		AbstractPersonAssocController<Task, TaskTO> {

	protected PersonTaskController() {
		super(Task.class, TaskTO.class);
	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PersonTaskController.class);

	@Autowired
	private transient TaskService service;

	@Autowired
	private transient TaskTOFactory factory;

	@Override
	protected TaskTOFactory getFactory() {
		return factory;
	}

	@Autowired
	private transient SecurityService securityService;

	/**
	 * Get All tasks for a given person, group them by their Task Group. A Task
	 * Group name is equivalent to the name of the Challenge associated with the
	 * Task. In the case of Custom Tasks, they are associated with a group named
	 * "Custom".
	 * 
	 * @param personId
	 *            Person identifier
	 * @param status
	 *            Object status
	 * @param start
	 *            First result index
	 * @param limit
	 *            Maximum amount of result rows to return
	 * @param sort
	 *            Sort property
	 * @param sortDirection
	 *            Sort direction
	 * @return Task lists in task groups.
	 * @throws ObjectNotFoundException
	 *             If the person identifier is not valid.
	 */
	@RequestMapping(value = "/group/", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<TaskTO>> getAllTasksWithTaskGroup(
			final @PathVariable UUID personId,
			final @RequestParam(required = false) ObjectStatus status,
			final @RequestParam(required = false) Integer start,
			final @RequestParam(required = false) Integer limit,
			final @RequestParam(required = false) String sort,
			final @RequestParam(required = false) String sortDirection)
			throws ObjectNotFoundException {

		final Map<String, List<TaskTO>> taskTOsWithTaskGroups = Maps
				.newTreeMap();

		final Map<String, List<Task>> tasksWithTaskGroups = service
				.getAllGroupedByTaskGroup(
						personService.get(personId),
						SortingAndPaging.createForSingleSort(status, start,
								limit, sort, sortDirection, null));

		for (Entry<String, List<Task>> tasksWithTaskGroup : tasksWithTaskGroups
				.entrySet()) {
			taskTOsWithTaskGroups.put(tasksWithTaskGroup.getKey(),
					TaskTO.toTOList(tasksWithTaskGroup.getValue()));
		}

		return taskTOsWithTaskGroups;
	}

	/**
	 * <p>
	 * Print the selected Tasks.
	 * </p>
	 * <p>
	 * If no tasks are selected, then just return the tasks for the person,
	 * (just for the session if it is the anon user).
	 * </p>
	 * 
	 * <p>
	 * Method Signature on this method will be changed to return a printable
	 * report
	 * </p>
	 * 
	 * @param personId
	 *            Person id
	 * @param taskIds
	 *            Task identifiers to retrieve
	 * @return All (or all specified) tasks for the specified Person
	 * @throws ObjectNotFoundException
	 *             If the Person id could not be found
	 */
	@RequestMapping(value = "/print/", method = RequestMethod.POST)
	public @ResponseBody
	List<TaskTO> print(
			final @PathVariable UUID personId,
			final @RequestBody List<UUID> taskIds)
			throws ObjectNotFoundException {

		final SortingAndPaging sAndP = new SortingAndPaging(ObjectStatus.ACTIVE);

		final List<Task> tasks = service.getTasksForPersonIfNoneSelected(
				taskIds, personService.get(personId),
				securityService.getSessionId(), sAndP);

		return TaskTO.toTOList(tasks);
	}

	/**
	 * <p>
	 * Email the selected tasks to addresses and/or people.
	 * </p>
	 * 
	 * <p>
	 * If no tasks are selected, then just return the tasks for the person,
	 * (just for the session if it is the anon user).
	 * </p>
	 * 
	 * @param personId
	 *            Person identifier
	 * @param emailForm
	 *            e-mail form data
	 * @return True if action was successful
	 * @throws Exception
	 *             If there were any exceptions.
	 */
	@RequestMapping(value = "/email/", method = RequestMethod.POST)
	public @ResponseBody
	boolean email(
			final @PathVariable UUID personId,
			final @RequestBody @Valid EmailPersonTasksForm emailForm)
			throws Exception {

		final Person student = personService.get(personId);

		final List<Task> tasks = service.getTasksForPersonIfNoneSelected(
				emailForm.getTaskIds(), student,
				securityService.getSessionId(),
				new SortingAndPaging(ObjectStatus.ACTIVE));

		final List<Person> recipients;

		if (emailForm.getRecipientIds() == null) {
			recipients = null;
		} else {
			// get a list of recipients from a list of recipientIds
			recipients = personService.peopleFromListOfIds(
					emailForm.getRecipientIds(),
					new SortingAndPaging(ObjectStatus.ACTIVE));
		}

		service.sendTasksForPersonToEmail(tasks, student,
				emailForm.getRecipientEmailAddresses(), recipients);

		return true;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected TaskService getService() {
		return service;
	}
}
