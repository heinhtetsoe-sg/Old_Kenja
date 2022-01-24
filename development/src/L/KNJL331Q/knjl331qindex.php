<?php

require_once('for_php7.php');

require_once('knjl331qModel.inc');
require_once('knjl331qQuery.inc');

class knjl331qController extends Controller
{
    public $ModelClassName = "knjl331qModel";
    public $ProgramID      = "KNJL331Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl331q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl331qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl331qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl331qForm1");
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
$knjl331qCtl = new knjl331qController();
