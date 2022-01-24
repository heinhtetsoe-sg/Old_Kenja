<?php

require_once('for_php7.php');

require_once('knjm430kModel.inc');
require_once('knjm430kQuery.inc');

class knjm430kController extends Controller {
    var $ModelClassName = "knjm430kModel";
    var $ProgramID      = "KNJM430K";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change":              //科目（講座）が変わったとき
                case "change_chaircd":      //学級・講座が変わったとき
                case "change_order":        //出力順が変わったとき
                case "reset":
                case "read":
                case "pre":
                case "next":
                    $sessionInstance->getMainModel();
                    $this->callView("knjm430kForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm430kCtl = new knjm430kController;
?>
