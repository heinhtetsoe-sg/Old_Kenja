<?php

require_once('for_php7.php');

require_once('knjb0052Model.inc');
require_once('knjb0052Query.inc');

class knjb0052Controller extends Controller
{
    public $ModelClassName = "knjb0052Model";
    public $ProgramID      = "KNJB0052";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjb0052":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb0052Model();        //コントロールマスタの呼び出し
                    $this->callView("knjb0052Form1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjb0052Form1");
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
$knjb0052Ctl = new knjb0052Controller();
//var_dump($_REQUEST);
