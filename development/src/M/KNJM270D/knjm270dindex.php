<?php

require_once('for_php7.php');

require_once('knjm270dModel.inc');
require_once('knjm270dQuery.inc');

class knjm270dController extends Controller {
    var $ModelClassName = "knjm270dModel";
    var $ProgramID      = "KNJM270D";     //プログラムID

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change":              //科目（講座）が変わったとき
                case "change_order":        //出力順が変わったとき
                case "reset":
                case "read":
                    $this->callView("knjm270dForm1");
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
$knjm270dCtl = new knjm270dController;
?>
