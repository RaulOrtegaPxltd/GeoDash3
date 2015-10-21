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

/**
 * Protect window.console method calls, e.g. console is not defined on IE unless dev tools are open, and IE doesn't define console.debug
 */
(function() {
	if (!window.console) {
		window.console = {};
	}
	// union of Chrome, FF, IE, and Safari console methods
	var i, m = [ "log", "info", "warn", "error", "debug", "trace", "dir", "group", "groupCollapsed", "groupEnd", "time", "timeEnd", "profile", "profileEnd", "dirxml", "assert", "count", "markTimeline", "timeStamp", "clear" ];
	// define undefined methods as noops to prevent errors
	for (i = 0; i < m.length; i) {
		if (!window.console[m[i]]) {
			window.console[m[i]] = function() {
			};
		}
		i += 1;
	}
})();

var loadCSSFile = function(url){
	// load styles
	var head = document.getElementsByTagName('head')[0], link = document.createElement('link');
	link.setAttribute('href', "url");
	link.setAttribute('rel', 'stylesheet');
	link.setAttribute('type', 'text/css');
	head.appendChild(link);	
};


var vis = null;

/**
 * Visualization definition
 */
(function() {
	// Custom mojo visualization requires Vis library to render, and in this
	// case LoadedExternalJSURLs to load 3rd party JS files
	mstrmojo.requiresCls("mstrmojo.Vis", "mstrmojo.LoadedExternalJSURLs", "mstrmojo._HasSelector");
	// Declaration of the plugin
	mstrmojo.plugins['Geodash3'] = mstrmojo.plugins['Geodash3'] || {};

	// declaring visualization globals
	mstrmojo.plugins.Geodash3['globals'] = mstrmojo.plugins.Geodash3['globals'] || {};

	// Declaration of the visualization object
	mstrmojo.plugins.Geodash3.Geodash3Vis = mstrmojo.plugins.Geodash3.Geodash3Vis || mstrmojo.declare(
	// superclass
	mstrmojo.CustomVisBase ? mstrmojo.CustomVisBase : mstrmojo.Vis,
	// mixins
	[ mstrmojo.LoadedExternalJSURLs, mstrmojo._HasSelector ], {
		scriptClass : 'mstrmojo.plugins.Geodash3.Geodash3Vis',
		model : null,
		/**
		 * markupString is a structure of a div to create as a placeholder for charts id is important since will be passed in to Google code as reference to div to append results
		 */
//		markupString : '<div id="GeoDash3Vis_{@id}"></div>',
		markupString : '<div id="geodash"></div>',
		/**
		 * code is ready lets prepare data
		 */
		postBuildRendering : function() {
			if (this._super) {
				this._super();
			}
			// Add META for special characters
			var meta = document.createElement('meta');
			meta.setAttribute('http-equiv', 'X-UA-Compatible');
			meta.setAttribute('content', 'IE=Edge');
			document.getElementsByTagName('head')[0].appendChild(meta);

			var meta = document.createElement('meta');
			meta.setAttribute('http-equiv', 'X-UA-Compatible');
			meta.setAttribute('content', 'chrome=1');
			document.getElementsByTagName('head')[0].appendChild(meta);

			
			// load browser compatibility styles
			// IE/Chrome
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/ie.css");
			// Firefox
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/ff.css");
			
			// load styles
			loadCSSFile("../plugins/Geodash3/style/GeoDash3.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/leaflet/leaflet.ie.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/leaflet/leaflet.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/jquery-ui-1.8.17.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/gd-core.css");
			loadCSSFile("../plugins/Geodash3/javascript/geodash-ui/css/colorpicker.css");

			// load js files
			this.load3rdPartyScripts();
		},
		/**
		 * load D3 visualization library
		 */
		load3rdPartyScripts : function() {
			// array of required JS files
			var scriptsObjectArray = [];
			scriptsObjectArray.push({url : "http://www.google.com/jsapi"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/lib/jquery-1.7.1.min.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/lib/underscore-min.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/lib/json2.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/lib/backbone-min.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/lib/jquery-ui-1.8.17.min.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/lib/colorpicker.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/lib/heatmap.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/lib/jquery.cookie.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.version.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.Base.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.Layer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.MarkerLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.MassMarkerLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.AreaLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.DssLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.KmlLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.HurricaneLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.EarthquakeLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.VectorLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.HeatmapLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.DirectionsLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.PlacesLayer.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/models/bdl.geodash.Layers.js"});
			
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Map.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.IconFactory.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.InfoBox.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Tools.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Selector.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Lasso.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.ContextMenu.js"});
			
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.KmlView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.HurricaneView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.EarthquakeView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.VectorView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.HeatmapView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.DirectionsView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MarkerView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MassMarkerView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.AreaView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.PlacesView.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.NavTab.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.GD.js"});	
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.Editor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MarkerEditor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MassMarkerEditor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.KmlEditor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.HurricaneEditor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.EarthquakeEditor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.VectorEditor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.HeatmapEditor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.AreaEditor.js"});
			scriptsObjectArray.push({url : "../plugins/Geodash3/javascript/geodash-ui/views/bdl.geodash.MSTR.js"});

			var me = this;
			// load required external JS files and after
			// that run
			// renderGraph method
			this.requiresExternalScripts(scriptsObjectArray, function() {
				// Load visualization with
				// callback to run when
				// the D3 Visualization API is
				// loaded.
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
			function resize(){
				if(window && window != window.top){
					var ht = $(window.parent).height()-2;
					var f = $("#bdl-map-frame",window.parent.document);
					ht = ht - f.offset().top;
					f.height(ht);
					$("#geodash").height(ht-10);
				}else{
					$("#geodash").height($(window).height());
				}
			};
			
			$(window.parent.document).ready(resize);
			   
			$(document).ready(function(){
				
					//base
					var base = "";
					
			    	window["gd"] = new bdl.geodash.GD(_.extend({
			    		el: document.getElementById("geodash")
			    	}, base));

			    	// layers
			    	gd.layers = [];

			    	if(gd.layers.length == 0){
			    		window.setTimeout(function(){
			    			gd.newLayer()},225);
			    	}
			});
			
			function resizeAll(){
				if(typeof(gd) != 'undefined') {
					resize();
					gd.resize();	    					
				} else {
					window.setTimeout(function(){resizeAll();},500);
				}			
			}
			
			$(window.parent).resize(resizeAll);


		},
		prepareData : function() {
			var results = [];

			var dp = this.getDataParser();
			dp = dp || this.getDataInterface();

			debugger;
			// Transform data from MicroStrategy.
			for (var i = 0; i < dp.getTotalRows(); i++) {
				//dp.getRowHeaders(i).getHeader(0).getName();
			}
			return results;
		}
	});
})();
