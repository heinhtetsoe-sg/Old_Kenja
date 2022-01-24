<?php

require_once('for_php7.php');

require_once('knja040aModel.inc');
require_once('knja040aQuery.inc');

class knja040aController extends Controller
{
    public $ModelClassName = "knja040aModel";
    public $ProgramID      = "KNJA040A";

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
                        $this->callView("knja040aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knja040aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJA040ACtl = new knja040aController();
