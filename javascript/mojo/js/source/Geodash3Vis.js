// //////////////////////////////////////////////////////////////////////////////////////////////////
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
		},
		submitEvents : function(events) {
			this.model.controller.model.slice({
				bufferedSlices : true,
				tks : (mstrmojo.hash.any(events) || {}).tks,
				events : events
			});
		},
		getEventForSelection : function(elementID, sc) {
			var m = this.getData();
			if(!sc)
				return null;
			var result = {
				ck : sc.ck,
				eid : elementID,
				src : m.k,
				tks : sc.tks,
				ctlKey : sc.ckey,
				include : true
			};
			return result;
		},
		getElementID : function(warehouseID) {
			var data = this.getData();
			var element = data.gts.row[0].es.filter(function(val, index, array) {
				return val.n === warehouseID;
			});
			return element && element.length >0 ? element[0].id : null;
		},
		getSC : function() {
			return this.getData().gts.row[0].sc;
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
		skipReRender : true,
		draggable: true,
		isDragValid: function isDragValid() {
		    return false;
		},
		shouldDragBubble: function shouldDragBubble() {
		    return true;
		},
		/**
		 * markupString is a structure of a div to create as a placeholder for charts id is important since will be passed in to Google code as reference to div to append results
		 */
		// markupString : '<div id="GeoDash3Vis_{@id}"></div>',
		// markupString : '<div id="geodash" style="z-index:50;"></div>',
		markupString : '<div class="custom-vis-layout {@cssClass}" style="position:absolute;left:{@left}px;top:{@top}px;overflow:hidden;width:{@width}px;height:{@height}px;z-index:{@zIndex};{@viewportCssText}">' + '<div id="geodash" style="width:{@width}px;height:{@height}px;z-index:{@zIndex};"></div>' + '</div>',
		/**
		 * code is ready lets prepare data
		 */
		postBuildRendering : function() {
			if (this._super) {
				this._super();
			}
			if (typeof (gd) != 'undefined') {
				if (this.domNode.childNodes.length > 0 && this.domNode.childNodes[0].childNodes.length == 0) { // Document
					var parent = this.domNode;
					parent.removeChild(this.domNode.firstChild);
					parent.appendChild(gd.el);
					gd.el.style.width = this.width + "px";
					gd.el.style.height = this.height + "px";
				}
				gd.resize();
				// We need to make sure the layer exist in the visualizations
				var verifyLayers = function(json) {
					var layers = json.layers
					if (typeof (layers) == 'undefined' || (layers && layers.length == 0)) {
						var currentLayers = gd.layers.models;
						for (i = 0; i < currentLayers.length; i++) {
							var action = bdl.geodash.MSTR.saveModel({
								model : currentLayers[i],
								success : null,
								error : null
							});
						}
					}
				}

				// layers
				taskInfo = {
					taskId : "geodash3GetJson",
					sessionState : mstrApp.sessionState,
					messageID : mstrApp.getMsgID(),
					gridKey : this.k
				};
				mstrmojo.xhr.request("POST", mstrConfig.taskURL, {
					success : verifyLayers,
					failure : function() {
						alert("An error ocurred");
					}
				}, taskInfo);

				return;
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
				me.renderGraph();
			});
		},

		/**
		 * Render Graph
		 */
		renderGraph : function() {
			this.model.docModel = mstrApp.docModel;
			if (!mstrApp.customVisualizations) {
				mstrApp.customVisualizations = [];
			}
			mstrApp.customVisualizations.push(this);
			if (!this.defn.vis) {
				this.defn.vis = {
					'vn' : "Geodash3MojoVisualizationStyle"
				};
			}
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
			var renderLayers = function(json) {
				var getScriptClass = function(type) {
					if (type == "markerLayer") {
						return "bdl.geodash.MarkerLayer";
					}
					if (type == "massMarkerLayer") {
						return "bdl.geodash.MassMarkerLayer";
					}
					if (type == "areaLayer") {
						return "bdl.geodash.AreaLayer";
					}
					if (type == "kmlLayer") {
						return "bdl.geodash.KmlLayer";
					}
					if (type == "vectorLayer") {
						return "bdl.geodash.VectorLayer";
					}
					if (type == "heatMapLayer" || type == "heatmapLayer") {
						return "bdl.geodash.HeatmapLayer";
					}
					if (type == "hurricaneLayer") {
						return "bdl.geodash.HurricaneLayer";
					}
					if (type == "dssLayer:earthquake") {
						return "bdl.geodash.DssLayer";
					}
				};
				var layers = json.layers;
				if (layers && layers.length && layers.length > 0) {
					for ( var i in layers) {
						var scriptClass = getScriptClass(layers[i].type);
						var ex = "var geoDashLayer = new " + scriptClass + "(" + JSON.stringify(layers[i]) + ");";
						eval(ex);
						gd.layers.add(geoDashLayer);
					}
				}
				if (gd.layers.length == 0) {
					window.setTimeout(function() {
						gd.newLayer()
					}, 225);
				}
			}

			var renderColumns = function(data) {
				if (data && data.columns && data.columns.length > 0) {
					for ( var i in data.columns) {
						gd.base.attributes.sourceColumns.push(data.columns[i]);
					}
				}
			}

			var renderGeodash = function(gdConfig) {
				// base
				var base = {
					'api' : gdConfig.webAPI,
					'mapType' : 0,
					'version' : gdConfig.version,
					'clientID' : gdConfig.googleClientID,
					'gdAPIKey' : gdConfig.geoDashAPIKey,
					'status' : 'ready',
					'sourceColumns' : [],
					// TODO: Set selector dynamically
					'selector' : "6FD68CD440A3269A507C139A52BEB6B4",
					'isBuilding' : false,
					'permissions' : {
						'edit' : true,
						'view' : true,
						'showLayerNavigator' : true
					},
					'isDoc' : true,
					'isSSL' : gdConfig.useSSL,
					'errors' : [],
					'parent' : vis
				};

				window["gd"] = new bdl.geodash.GD(_.extend({
					el : vis.domNode.firstChild,
				}, base));

				// columns
				var taskInfo = {
					taskId : "geodash3GetLayerColumns",
					sessionState : mstrApp.sessionState,
					messageID : mstrApp.getMsgID(),
					gridKey : vis.k
				};

				mstrmojo.xhr.request("POST", mstrConfig.taskURL, {
					success : renderColumns,
					failure : function() {
						alert("An error occurred");
					}
				}, taskInfo);

				// layers
				taskInfo = {
					taskId : "geodash3GetJson",
					sessionState : mstrApp.sessionState,
					messageID : mstrApp.getMsgID(),
					gridKey : vis.k
				};
				mstrmojo.xhr.request("POST", mstrConfig.taskURL, {
					success : renderLayers,
					failure : function() {
						alert("An error occurred");
					}
				}, taskInfo);
			};

			// Call validation task validateGeodashPrivileges passing the sessionState : mstrApp.sessionState
			mstrmojo.xhr.request("POST", mstrConfig.taskURL, {
				success : renderGeodash,
				failure : function() {
					alert("An error occurred");
				}
			}, {
				taskId : "geodash3GetSettings",
				sessionState : mstrApp.sessionState
			});

		},
		prepareData : function() {
			var results = [];
			return results;
		}
	});
})();