/**
 * @license Copyright (c) 2003-2014, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
    config.language = 'zh-cn';
    config.removePlugins = 'print,save';
    config.toolbarCanCollapse = false; 
    config.resize_enabled = false;
    config.protectedSource.push(/<\s*iframe[\s\S]*?>/gi); 
};
