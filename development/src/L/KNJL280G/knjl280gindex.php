<?php

require_once('for_php7.php');

require_once('knjl280gModel.inc');
require_once('knjl280gQuery.inc');

class knjl280gController extends Controller
{
    public $ModelClassName = "knjl280gModel";
    public $ProgramID      = "KNJL280G";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "uproad":
                    $sessionInstance->getUproadModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear";
                    $this->callView("knjl280gForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl280gCtl = new knjl280gController();
//var_dump($_REQUEST);
