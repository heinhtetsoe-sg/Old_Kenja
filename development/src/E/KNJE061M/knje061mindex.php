<?php

require_once('for_php7.php');

require_once('knje061mModel.inc');
require_once('knje061mQuery.inc');

class knje061mController extends Controller {
    var $ModelClassName = "knje061mModel";
    var $ProgramID      = "KNJE061M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "coursecode":
                case "hr_class":
                case "annual":
                    $sessionInstance->getMainModel();
                    $this->callView("knje061mForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knje061mCtl = new knje061mController;
?>
