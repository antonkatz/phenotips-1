/**
 * The idea of this code is to achieve 2 goals.
 * 1. Provide convenient direct and indirect methods for access and manipulation of the data in PhenotypeMetaClass (XWiki).
 * 2. Provide helper methods for integration of [1] into the existing code base.
 *
 * [2] is temporary, until the time when PatientSheetCode (and other code) can be directly integrated with [1].
 *
 * It has been decided that data should be fetched from REST, and avoid the middle layer of xwiki completely.
 */

require(['jquery'], function ($)
{
    window.PhenoTips = function (_PhenoTips)
    {

        //If phenotype exists and is not empty do not continue
        if (_PhenoTips.phenotype) {
            return _PhenoTips;
        }
        //Core - programmatic access to data
        //Display - visual representation of the data with observers
        //Rest - functions relating to xwiki rest usage
        //LegacyIntegration - some functions that should not be part of the code, but are required due to old code
        //Configuration - should eventually be merged into a global configuration
        var phenotype = _PhenoTips.phenotype =
        {core: {}, display: {}, rest: {}, legacyIntegration: {}, configuration: {}};
        var core = phenotype.core;
        var display = phenotype.display;
        var rest = phenotype.rest;
        var integration = phenotype.legacyIntegration;
        var config = phenotype.configuration;

        /**
         * General Notes:
         *
         */

        //Retrieves all phenotypes available in the patient document.
        rest.getPhenotypes = function (){

        };


        return _PhenoTips;
    }(window.PhenoTips || {})
    console.log(window.PhenoTips);
});

///bin/objectadd/data/P0000001?classname=PhenoTips.PhenotypeMetaClass&PhenoTips.PhenotypeMetaClass_target_property_name=phenotype&PhenoTips.PhenotypeMetaClass_target_property_value=HP:0004325&xredirect=%2Fbin%2Fedit%2Fdata%2FP0000001%3Fxaction%3Dlastmeta%26xpage%3Dplain&form_token=uDjuGHgZR5eMOMPhbIOB2w
