<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knja145index.php 56585 2017-10-22 12:47:53Z maeshiro $

require_once('knja145Model.inc');
require_once('knja145Query.inc');

class knja145Controller extends Controller {
    var $ModelClassName = "knja145Model";
    var $ProgramID      = "KNJA145";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "output":
                case "knja145":
                    $sessionInstance->knja145Model();
                    $this->callView("knja145Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja145Ctl = new knja145Controller;
//var_dump($_REQUEST);
?>
