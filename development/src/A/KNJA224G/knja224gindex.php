<?php

require_once('for_php7.php');

require_once('knja224gModel.inc');
require_once('knja224gQuery.inc');

class knja224gController extends Controller
{
    public $ModelClassName = "knja224gModel";
    public $ProgramID      = "KNJA224G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja224g":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja224gModel();        //コントロールマスタの呼び出し
                    $this->callView("knja224gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja224gCtl = new knja224gController();
