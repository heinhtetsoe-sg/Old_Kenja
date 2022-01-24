<?php

require_once('for_php7.php');

require_once('knjz350v_2Model.inc');
require_once('knjz350v_2Query.inc');

class knjz350v_2Controller extends Controller {
    var $ModelClassName = "knjz350v_2Model";
    var $ProgramID      = "KNJZ350V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ350V_2");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                case "subclasscd";
                    $sessionInstance->setAccessLogDetail("S", "KNJZ350V_2");
                    $this->callView("knjz350v_2Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz350v_2Ctl = new knjz350v_2Controller;
//var_dump($_REQUEST);
?>
