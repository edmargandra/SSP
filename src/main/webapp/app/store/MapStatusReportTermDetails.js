/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
Ext.define('Ssp.store.MapStatusReportTermDetails', {
    extend: 'Ext.data.Store',
    model: 'Ssp.model.tool.map.MapStatusTermDetail',
    mixins: [ 'Deft.mixin.Injectable' ],
    inject: {
        apiProperties: 'apiProperties'
    },
	constructor: function(){
		Ext.apply(this, {
							proxy: this.apiProperties.getProxy(this.apiProperties.getItemUrl('mapStatusReport')+'/termDetails'),
							autoLoad: false
						});
		return this.callParent(arguments);
	},
	load: function(id)
	{
		var me=this;
		
		var successFunc = function(response){
	    	var r;
	    	r = Ext.decode(response.responseText);
	    		me.loadData(r);
		};
		
		me.studentDocumentUrl = me.apiProperties.getItemUrl('mapStatusReport')+'/termDetails';
		me.studentDocumentUrl = me.studentDocumentUrl.replace('{id}',id);
		me.apiProperties.makeRequest({
			url: me.apiProperties.createUrl(me.studentDocumentUrl),
			method: 'GET',
			successFunc: successFunc 
		});
	}
});