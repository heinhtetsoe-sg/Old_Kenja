<?php

require_once('for_php7.php');

require_once('knjl210bModel.inc');
require_once('knjl210bQuery.inc');

class knjl210bController extends Controller {
    var $ModelClassName = "knjl210bModel";
    var $ProgramID      = "KNJL210B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "exec":
                    if (!$sessionInstance->OutputDataFile()) {
                        $this->callView("knjl210bForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl210bForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl210bCtl = new knjl210bController;
?>
