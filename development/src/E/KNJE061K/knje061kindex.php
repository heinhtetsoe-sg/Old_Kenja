<?php

require_once('for_php7.php');

require_once('knje061kModel.inc');
require_once('knje061kQuery.inc');

class knje061kController extends Controller
{
    public $ModelClassName = "knje061kModel";
    public $ProgramID      = "KNJE061K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("updMain");
                    break 1;
                case "":
                case "updMain":
                case "main":
                case "coursecode":
                case "hr_class":
                case "annual":
                    $sessionInstance->getMainModel();
                    $this->callView("knje061kForm1");
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
$knje061kCtl = new knje061kController();
