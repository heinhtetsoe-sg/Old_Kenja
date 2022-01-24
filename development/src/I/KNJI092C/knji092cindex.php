<?php

require_once('for_php7.php');

require_once('knji092cModel.inc');
require_once('knji092cQuery.inc');

class knji092cController extends Controller
{
    public $ModelClassName = "knji092cModel";
    public $ProgramID      = "KNJI092C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji092c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knji092cModel();      //コントロールマスタの呼び出し
                    $this->callView("knji092cForm1");
                    exit;
                case "csvOutput":   //CSV出力
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knji092cForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji092cCtl = new knji092cController();
