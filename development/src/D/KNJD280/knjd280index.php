<?php

require_once('for_php7.php');

require_once('knjd280Model.inc');
require_once('knjd280Query.inc');

class knjd280Controller extends Controller
{
    public $ModelClassName = "knjd280Model";
    public $ProgramID      = "KNJD280";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd280":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd280Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd280Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd280Form1");
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
$knjd280Ctl = new knjd280Controller();
//var_dump($_REQUEST);
