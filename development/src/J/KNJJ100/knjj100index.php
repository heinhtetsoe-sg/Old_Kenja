<?php

require_once('for_php7.php');

require_once('knjj100Model.inc');
require_once('knjj100Query.inc');

class knjj100Controller extends Controller
{
    public $ModelClassName = "knjj100Model";
    public $ProgramID      = "KNJJ100";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj100":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj100Model();       //コントロールマスタの呼び出し
                    $this->callView("knjj100Form1");
                    exit;
                case "csv":         //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjj100Form1");
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
$knjj100Ctl = new knjj100Controller();
//var_dump($_REQUEST);
