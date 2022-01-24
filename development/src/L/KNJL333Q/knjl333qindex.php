<?php

require_once('for_php7.php');

require_once('knjl333qModel.inc');
require_once('knjl333qQuery.inc');

class knjl333qController extends Controller
{
    public $ModelClassName = "knjl333qModel";
    public $ProgramID      = "KNJL333Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl333q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl333qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl333qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl333qForm1");
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
$knjl333qCtl = new knjl333qController();
