////////////////////////////////////////////////////////////////////////////////////////////////////
// _HasSelector.js

(function() {

	/**
	 * <p>
	 * A mixin for creating the supporting Selector action from visualization.
	 * <p>
	 * 
	 * @mixin
	 */
	mstrmojo._HasSelector = mstrmojo.provide('mstrmojo._HasSelector',

	/**
	 * @lends mstrmojo._HasSelector.prototype
	 */
	{
		_mixinName : 'mstrmojo._HasSelector',
		/**
		 * Prepare data for this mixin
		 */
		postBuildRendering : function() {
			if (this._super) {
				this._super();
			}
			if (!this.dfm) {

				var ts = this.model.gts;
				if (ts) {
					var tts = (ts.row || []).concat(ts.col || []);
					var i = 0, tCnt = 0, fCnt = 0, len = tts && tts.length, t = null, r = null;
					for (; i < len; i++) {
						t = tts[i];
						if (t.otp != -1) {
							tCnt++;
							if (t.es) {
								fCnt++;
								r = t;
							}
						}
					}
					this.dfm = (fCnt == 1 && tCnt == 1) ? r : null;
				}
			}
		},
		/**
		 * Handle simple selector action
		 * 
		 * @param {string}
		 *            Attribute element id, should be as from DataFormater
		 */
		makeSelection : function(attElementID) {
			var cnt = this, m = this.model, sc = this.dfm && this.dfm.sc;
			if (window.microstrategy && window.microstrategy.findBone) {
				// we are on Interactive mode
				var grid = microstrategy.findBone(cnt.domNode);
				if (grid && grid.makeSelections) {
					grid.makeSelections([ {
						attId : cnt.dfm.id,
						values : [ attElementID ]
					} ]);
				}
			} else {
				var dm = this.model.docModel;
				var m = this.model, ifws = dm.getTargetInfoWin(sc.tks);
				if (ifws && ifws.length) {
					for (var i = 0; i < ifws.length; i++) {
						dm.showInfoWin(ifws[i], sc.anchor, "h", true);
					}
				}
				if (dm && dm.slice) {
					dm.slice({
						ck : sc.ck,
						eid : attElementID,
						src : m.k,
						tks : sc.tks,
						ctlKey : sc.ckey,
						include : true
					});
				}
			}
		},
		/**
		 * Check if given element is currently selected in selector functionality
		 * 
		 * @param {string}
		 *            value should be as from DataFormater
		 * @return {bool} is element selected
		 */
		isElementSelected : function(attElementID) {
			if (!this.idSelected) {
				this.idSelected = -1;
				if (this.dfm && this.dfm.sc) {
					if (this.dfm.sc.ces && this.dfm.sc.ces.length > 0) {
						var idx = this.dfm.sc.ces[0].id;
						this.idSelected = parseInt(idx.substr(idx.lastIndexOf(":") + 1));
						if (isNaN(this.idSelected)) {
							idx = idx.substr(0, idx.lastIndexOf(":"));
							this.idSelected = parseInt(idx.substr(idx.lastIndexOf(":") + 1));
						}
					}
				}
			}
			attElementID = attElementID.substr(0, attElementID.lastIndexOf(":"));
			attElementID = parseInt(attElementID.substr(attElementID.lastIndexOf(":") + 1));
			return attElementID == this.idSelected;
		}
	});
}());
// //////////////////////////////////////////////////////////////////////////////////////////////////

var loadCSSFile = function(url) {
	// load styles
	var head = document.getElementsByTagName('head')[0], link = document.createElement('link');
	link.setAttribute('href', url);
	link.setAttribute('rel', 'stylesheet');
	link.setAttribute('type', 'text/css');
	head.appendChild(link);
};

var vis = null;
var vis3rdPartyFramework = "Geodash3";
var visName = "Geodash3Vis";

/**
 * Visualization definition
 */
(function() {
	// Custom mojo visualization requires Vis library to render, and in this
	// case LoadedExternalJSURLs to load 3rd party JS files
	mstrmojo.requiresCls("mstrmojo.VisBase", "mstrmojo._LoadsScript", "mstrmojo.models.template.DataInterface", "mstrmojo._HasSelector");
	// Declaration of the plugin
	mstrmojo.plugins[vis3rdPartyFramework] = mstrmojo.plugins[vis3rdPartyFramework] || {};

	// declaring visualization globals
	mstrmojo.plugins[vis3rdPartyFramework]['globals'] = mstrmojo.plugins[vis3rdPartyFramework]['globals'] || {};

	// Declaration of the visualization object
	mstrmojo.plugins[vis3rdPartyFramework][visName] = mstrmojo.plugins[vis3rdPartyFramework][visName] || mstrmojo.declare(
	// superclass
	mstrmojo.VisBase,
	// mixins
	[ mstrmojo._LoadsScript, mstrmojo._HasSelector ], {
		scriptClass : 'mstrmojo.plugins.' + vis3rdPartyFramework + '.' + visName,
		model : null,
		/**
		 * markupString is a structure of a div to create as a placeholder for charts id is important since will be passed in to Google code as reference to div to append results
		 */
		// markupString : '<div id="GeoDash3Vis_{@id}"></div>',
		markupString : '<div id="geodash"></div>',
		/**
		 * code is ready lets prepare data
		 */
		postBuildRendering : function() {
			if (this._super) {
				this._super();
			}
			// load browser compatibility styles
			// IE/Chrome
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/ie.css");
			// Firefox
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/ff.css");

			// load styles
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/leaflet/leaflet.ie.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/leaflet/leaflet.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/jquery-ui-1.8.17.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/gd-core.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/colorpicker.css");

			// load js files
			this.loadScripts();
		},
		loadScripts : function() {
			// array of required JS files
			var scriptsObjectArray = [];
			scriptsObjectArray.push({
				url : "http://www.google.com/jsapi"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/lib/jquery-1.7.1.min.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/lib/underscore-min.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/lib/json2.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/lib/backbone-min.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/lib/jquery-ui-1.8.17.min.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/lib/colorpicker.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/lib/heatmap.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/lib/jquery.cookie.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.version.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.Base.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.Layer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.MarkerLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.MassMarkerLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.AreaLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.DssLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.KmlLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.HurricaneLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.EarthquakeLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.VectorLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.HeatmapLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.DirectionsLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.PlacesLayer.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.Layers.js"
			});

			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Map.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.IconFactory.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.InfoBox.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Tools.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Selector.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Lasso.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.ContextMenu.js"
			});

			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.KmlView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.HurricaneView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.EarthquakeView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.VectorView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.HeatmapView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.DirectionsView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MarkerView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MassMarkerView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.AreaView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.PlacesView.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.NavTab.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.GD.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Editor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MarkerEditor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MassMarkerEditor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.KmlEditor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.HurricaneEditor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.EarthquakeEditor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.VectorEditor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.HeatmapEditor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.AreaEditor.js"
			});
			scriptsObjectArray.push({
				url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MSTR.js"
			});

			var me = this;
			// load required external JS files and after
			// that run
			// renderGraph method
			this.requiresExternalScripts(scriptsObjectArray, function() {
				// Load visualization with
				// callback to run when
				// the D3 Visualization API is
				// loaded.
				debugger;
				me.renderGraph();
			});
		},

		/**
		 * Render Graph
		 */
		renderGraph : function() {

			this.model.docModel = mstrApp.docModel;
			vis = this;

			var data = this.prepareData();

			// <!--MICROSTRATEGY DASHBOARD PLACEMENT

			var visContainer = this.domNode;

			if (this.top != Number.NaN && this.left != Number.NaN) {
				visContainer.style.top = ("" + this.top).indexOf("px") > -1 ? this.top : this.top + "px";
				visContainer.style.left = ("" + this.left).indexOf("px") > -1 ? this.left : this.left + "px";
				visContainer.style.position = "absolute";
			}
			visContainer.style.width = ("" + this.width).indexOf("px") > -1 ? this.width : this.width + "px";
			visContainer.style.height = ("" + this.height).indexOf("px") > -1 ? this.height : this.height + "px";
			visContainer.style.overflow = 'hidden';

			// VISUALIZATION CODE
			// Call validation task validateGeodashPrivileges passing the sessionState : mstrApp.sessionState

			var renderGeodash = function() {
//				$(document).ready(function() {
					// base
				
					debugger;
					var base = 		{
							'api': 'google',
							'mapType': 0,
							'version': '2.0.0',
							'clientID': 'gme-projectxlabsltd',
							'gdAPIKey': 'AIzaSyDnWbzEK6ztazKoOh2C292439ixcmdkc88',
							'status': 'ready',
							'sourceColumns': [],
							// [{'name':'','type':'attribute','attID':''}{'name':'','type':'metric'}]
							'isSSL': false,
							'errors':[]
					};

					window["gd"] = new bdl.geodash.GD(_.extend({
						el : vis.domNode
					}, base));

					// layers
					gd.layers = [];

					if (gd.layers.length == 0) {
						window.setTimeout(function() {
							gd.newLayer()
						}, 225);
					}
//				});
			};
			renderGeodash();
		},
		prepareData : function() {
			var results = [];
			return results;
		}
	});
})();
