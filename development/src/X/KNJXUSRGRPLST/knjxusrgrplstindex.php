<?php
require_once('knjxusrgrplstModel.inc');
require_once('knjxusrgrplstQuery.inc');

class knjxusrgrplstController extends Controller {
    var $ModelClassName = "knjxusrgrplstModel";
    var $ProgramID      = "KNJXUSRGRPLST";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change_kind":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjxusrgrplstForm1");
                   break 2;
                case "error":
                    $this->callView("error");
                case "":
                      $sessionInstance->setCmd("main");
                    break 1;
  //                  return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxusrgrplstCtl = new knjxusrgrplstController;
//var_dump($_REQUEST);
?>
