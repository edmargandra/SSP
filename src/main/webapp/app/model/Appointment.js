Ext.define('Ssp.model.Appointment', {
    extend: 'Ssp.model.AbstractBase',
    fields: [{name:'appointmentDate', type: 'date', dateFormat: 'time'},
             {name: 'startTime', type: 'date', dateFormat: 'time'},
             {name: 'endTime', type: 'date', dateFormat: 'time'}],

    getStartDate: function(){
		var me=this;
    	var startDate = new Date( me.get('appointmentDate') );
		startDate.setHours( me.get('startTime').getHours() );
		startDate.setMinutes( me.get('startTime').getMinutes() );
		return startDate;
    },
    
    getEndDate: function(){
    	var me=this;
    	var endDate = new Date( me.get('appointmentDate') );
		endDate.setHours( me.get('endTime').getHours() );
		endDate.setMinutes( me.get('endTime').getMinutes() );
		return endDate;    	
    }
});