<?php

require_once('for_php7.php');

require_once('knja142cModel.inc');
require_once('knja142cQuery.inc');

class knja142cController extends Controller
{
    public $ModelClassName = "knja142cModel";
    public $ProgramID      = "KNJA142C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja142c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja142cModel();        //コントロールマスタの呼び出し
                    $this->callView("knja142cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja142cCtl = new knja142cController();
