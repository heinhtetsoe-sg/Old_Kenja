<?php

require_once('for_php7.php');
require_once('knjb103fModel.inc');
require_once('knjb103fQuery.inc');

class knjb103fController extends Controller
{
    public $ModelClassName = "knjb103fModel";
    public $ProgramID      = "KNJB103F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjb103fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjb103fForm1");
                    exit;
                case "knjb103f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjb103fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjb103fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb103fCtl = new knjb103fController;
//var_dump($_REQUEST);
