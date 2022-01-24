<?php

require_once('for_php7.php');

require_once('knjd219cModel.inc');
require_once('knjd219cQuery.inc');

class knjd219cController extends Controller
{
    public $ModelClassName = "knjd219cModel";
    public $ProgramID      = "KNJD219C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "sim":
                    $sessionInstance->getSimModel();
                    $this->callView("knjd219cForm1");
                    break 2;
                case "standard":
                    $sessionInstance->getStandardModel();
                    $this->callView("knjd219cForm1");
                    break 2;
                case "inquiry":
                    $this->callView("knjd219cForm2");
                    break 2;
                case "clear":
                case "change":
                case "main":
                case "default":
                case "":
                    $this->callView("knjd219cForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd219cCtl = new knjd219cController();
//var_dump($_REQUEST);
