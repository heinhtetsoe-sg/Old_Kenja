<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjb130index.php 56585 2017-10-22 12:47:53Z maeshiro $

require_once('knjb130Model.inc');
require_once('knjb130Query.inc');

class knjb130Controller extends Controller {
    var $ModelClassName = "knjb130Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb130":
                    $sessionInstance->knjb130Model();
                    $this->callView("knjb130Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjb130Ctl = new knjb130Controller;
//var_dump($_REQUEST);
?>
