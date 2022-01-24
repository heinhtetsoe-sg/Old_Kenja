<?php

require_once('for_php7.php');

require_once('knjs343Model.inc');
require_once('knjs343Query.inc');

class knjs343Controller extends Controller
{
    public $ModelClassName = "knjs343Model";
    public $ProgramID      = "KNJS343";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "reset":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjs343Form1");
                    break 2;
                case "knjs343":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjs343Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs343Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs343Ctl = new knjs343Controller();
