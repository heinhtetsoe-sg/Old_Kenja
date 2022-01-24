<?php
require_once('knja240gModel.inc');
require_once('knja240gQuery.inc');

class knja240gController extends Controller
{
    public $ModelClassName = "knja240gModel";
    public $ProgramID      = "KNJA240G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja240g":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja240gModel();      //コントロールマスタの呼び出し
                    $this->callView("knja240gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja240gCtl = new knja240gController();
