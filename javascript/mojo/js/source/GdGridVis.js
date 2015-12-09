var vis = null;
var vis3rdPartyFramework = "Geodash3";
var visName = "GdGridVis";

/**
 * Visualization definition
 */
(function() {
	// Custom mojo visualization requires Vis library to render, and in this
	// case LoadedExternalJSURLs to load 3rd party JS files
	mstrmojo.requiresCls("mstrmojo.VisBase", "mstrmojo._LoadsScript", "mstrmojo.models.template.DataInterface");
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
		markupString : '<div id="GgGrid_{@id}"></div>',
		/**
		 * code is ready lets prepare data
		 */
		postBuildRendering : function() {
			if (this._super) {
				this._super();
			}

			// load js files
			this.loadScripts();
		},
		loadScripts : function() {
			// array of required JS files
			var scriptsObjectArray = [];

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
			if(!mstrApp.customVisualizations){
				mstrApp.customVisualizations = [];
			}
			mstrApp.customVisualizations.push(this);
			if(!this.defn.vis){
				this.defn.vis = {'vn':"GdGridMojoVisualizationStyle"};
			}
			this.defn.parent = this;
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
			
			
		},
		prepareData : function() {
			var results = [];
			return results;
		}
	});
})();
