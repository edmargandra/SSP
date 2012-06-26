Ext.define('Ssp.controller.tool.profile.ProfilePersonViewController', {
    extend: 'Deft.mvc.ViewController',
    mixins: [ 'Deft.mixin.Injectable' ],
    inject: {
    	apiProperties: 'apiProperties',
    	appEventsController: 'appEventsController',
        person: 'currentPerson',
        profileSpecialServiceGroupsStore: 'profileSpecialServiceGroupsStore',
        profileReferralSourcesStore: 'profileReferralSourcesStore',
        sspConfig: 'sspConfig'
    },
    
    control: {
    	nameField: '#studentName',
    	studentIdField: '#studentId',
    	birthDateField: '#birthDate'
    },
	init: function() {
		var me=this;
		me.getView().getForm().reset();
		var studentRecordComp = Ext.ComponentQuery.query('.studentrecord')[0];
		var studentIdField = me.getStudentIdField();
		var nameField = me.getNameField();
		var birthDateField = me.getBirthDateField();
		var id= me.person.get('id');
		var personUrl = me.apiProperties.createUrl( me.apiProperties.getItemUrl('person') );
		var studentIdAlias = me.sspConfig.get('studentIdAlias');			
		var successFunc = function(response,view){
	    	var r = Ext.decode(response.responseText);
    		var fullName;
	    	
    		// load the person data
    		me.person.populateFromGenericObject(r);
    		
	    	fullName = me.person.getFullName();
    		
    		// load special service groups
    		if (r.specialServiceGroups != null)
    		{
    			me.profileSpecialServiceGroupsStore.loadData( me.person.get('specialServiceGroups') );
    		}else{
    			me.profileSpecialServiceGroupsStore.removeAll();
    		}
    		
    		// load referral sources
    		if (r.referralSources != null)
    		{
    			me.profileReferralSourcesStore.loadData( me.person.get('referralSources') );
    		}else{
    			me.profileReferralSourcesStore.removeAll();    			
    		}
    		
    		// load general student record
    		me.getView().loadRecord( me.person );
    		
    		// load additional values
    		nameField.setValue( fullName );
    		birthDateField.setValue( me.person.getFormattedBirthDate() );
    		studentRecordComp.setTitle('Student Record - ' + fullName)
		};

		// Set defined configured label for the studentId field
		studentIdField.setFieldLabel(studentIdAlias);		
		
		// load the person record
		me.apiProperties.makeRequest({
			url: personUrl+id,
			method: 'GET',
			successFunc: successFunc 
		});
		
		return this.callParent(arguments);
    }
});