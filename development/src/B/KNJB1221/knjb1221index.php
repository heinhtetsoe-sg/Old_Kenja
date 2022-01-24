<?php

require_once('for_php7.php');

require_once('knjb1221Model.inc');
require_once('knjb1221Query.inc');

class knjb1221Controller extends Controller {
    var $ModelClassName = "knjb1221Model";
    var $ProgramID      = "KNJB1221";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "changeCourse":
                case "changeHr":
                case "changeStudent":
                case "changeCopyChairDate":
                case "changeTrgtHr":
                case "changeDate":
                    $sessionInstance->knjb1221Model();      //コントロールマスタの呼び出し
                    $this->callView("knjb1221Form1");
                    exit;
                case "update":
                    if (!$sessionInstance->getUpdateModel()){
                        $this->callView("knjb1221Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1221Ctl = new knjb1221Controller;
?>
