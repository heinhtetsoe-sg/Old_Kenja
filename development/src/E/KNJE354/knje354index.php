<?php

require_once('for_php7.php');

require_once('knje354Model.inc');
require_once('knje354Query.inc');

class knje354Controller extends Controller
{
    public $ModelClassName = "knje354Model";
    public $ProgramID      = "KNJE354";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
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
                        $this->callView("knje354Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knje354Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJE354Ctl = new knje354Controller();
