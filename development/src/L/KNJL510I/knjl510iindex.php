<?php
require_once('knjl510iModel.inc');
require_once('knjl510iQuery.inc');

class knjl510iController extends Controller
{
    public $ModelClassName = "knjl510iModel";
    public $ProgramID      = "KNJL510I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reference":
                case "back1":
                case "next1":
                    $this->callView("knjl510iForm1");
                    break 2;
                case "back":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("back1");
                    break 1;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("next1");
                    break 1;
                case "add":
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "chagneSort":
                    $this->callView("knjl510iForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl510iCtl = new knjl510iController();
