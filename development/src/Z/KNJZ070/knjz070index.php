<?php

require_once('for_php7.php');

require_once('knjz070Model.inc');
require_once('knjz070Query.inc');

class knjz070Controller extends Controller
{
    public $ModelClassName = "knjz070Model";
    public $ProgramID      = "KNJZ070";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                case "clear":
                case "btn_def":
                    $this->callView("knjz070Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz070Ctl = new knjz070Controller();
//var_dump($_REQUEST);
