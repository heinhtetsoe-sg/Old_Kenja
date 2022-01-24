<?php

require_once('for_php7.php');

require_once('knja224Model.inc');
require_once('knja224Query.inc');

class knja224Controller extends Controller
{
    public $ModelClassName = "knja224Model";
    public $ProgramID      = "KNJA224";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja224":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja224Model();        //コントロールマスタの呼び出し
                    $this->callView("knja224Form1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja224Form1");
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
$knja224Ctl = new knja224Controller();
