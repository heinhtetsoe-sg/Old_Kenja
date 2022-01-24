<?php

require_once('for_php7.php');

require_once('knjc230Model.inc');
require_once('knjc230Query.inc');

class knjc230Controller extends Controller
{
    public $ModelClassName = "knjc230Model";
    public $ProgramID      = "KNJC230";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->knjc230Model();     //コントロールマスタの呼び出し
                    $this->callView("knjc230Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc230Ctl = new knjc230Controller();
//var_dump($_REQUEST);
