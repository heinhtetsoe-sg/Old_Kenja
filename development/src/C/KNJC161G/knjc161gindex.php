<?php

require_once('for_php7.php');

require_once('knjc161gModel.inc');
require_once('knjc161gQuery.inc');

class knjc161gController extends Controller
{
    public $ModelClassName = "knjc161gModel";
    public $ProgramID      = "KNJC161G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "semester":
                case "knjc161g":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc161gModel();      //コントロールマスタの呼び出し
                    $this->callView("knjc161gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc161gCtl = new knjc161gController();
