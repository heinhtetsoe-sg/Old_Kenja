<?php

require_once('for_php7.php');
require_once('knja612Model.inc');
require_once('knja612Query.inc');

class knja612Controller extends Controller
{
    public $ModelClassName = "knja612Model";
    public $ProgramID      = "KNJA612";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja612":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja612Model();       //コントロールマスタの呼び出し
                    $this->callView("knja612Form1");
                    exit;
                case "cancel":
                    $this->callView("knja612Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->updateScore()) {
                        $this->callView("knja612Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja612Ctl = new knja612Controller();
