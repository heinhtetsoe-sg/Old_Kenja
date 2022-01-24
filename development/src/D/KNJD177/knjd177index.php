<?php

require_once('for_php7.php');

require_once('knjd177Model.inc');
require_once('knjd177Query.inc');

class knjd177Controller extends Controller
{
    public $ModelClassName = "knjd177Model";
    public $ProgramID      = "KNJD177";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd177":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd177Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd177Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd177Form1");
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
$knjd177Ctl = new knjd177Controller();
//var_dump($_REQUEST);
