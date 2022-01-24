<?php

require_once('for_php7.php');

require_once('knja050Model.inc');
require_once('knja050Query.inc');

class knja050Controller extends Controller
{
    public $ModelClassName = "knja050Model";
    public $ProgramID      = "KNJA050";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "subExecute": //年度確定処理
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update": //台帳番号採番処理
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getOneUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "subForm1":
                    $this->callView("knja050SubForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "execute": //年度確定処理
                    $this->callView("knja050Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja050Ctl = new knja050Controller();
//var_dump($_REQUEST);
