<?php

require_once('for_php7.php');

require_once('knja040bModel.inc');
require_once('knja040bQuery.inc');

class knja040bController extends Controller
{
    public $ModelClassName = "knja040bModel";
    public $ProgramID      = "KNJA040B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "output":
                    if (!$sessionInstance->outputTmpFile()) {
                        $this->callView("knja040bForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "schoolKind":
                case "main":
                    $this->callView("knja040bForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJA040BCtl = new knja040bController();

//var_dump($_REQUEST);
