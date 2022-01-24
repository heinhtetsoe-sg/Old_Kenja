<?php

require_once('for_php7.php');

require_once('knjl151kModel.inc');
require_once('knjl151kQuery.inc');

class knjl151kController extends Controller
{
    public $ModelClassName = "knjl151kModel";
    public $ProgramID      = "KNJL151K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knjl151kForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl151kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJL151KCtl = new knjl151kController();
