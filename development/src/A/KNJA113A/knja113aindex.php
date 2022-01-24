<?php

require_once('for_php7.php');

require_once('knja113aModel.inc');
require_once('knja113aQuery.inc');

class knja113aController extends Controller {
    var $ModelClassName = "knja113aModel";
    var $ProgramID      = "KNJA113A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "stdDivChange":
                case "schoolKindChange":
                case "scholarshipChange":
                case "knja113a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja113aModel();       //コントロールマスタの呼び出し
                    $this->callView("knja113aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja113aCtl = new knja113aController;
//var_dump($_REQUEST);
?>
