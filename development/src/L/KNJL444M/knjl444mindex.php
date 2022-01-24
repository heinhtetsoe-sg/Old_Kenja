<?php

require_once('for_php7.php');

require_once('knjl444mModel.inc');
require_once('knjl444mQuery.inc');

class knjl444mController extends Controller
{
    public $ModelClassName = "knjl444mModel";
    public $ProgramID        = "KNJL444M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl444m":    //メニュー画面もしくはSUBMITした場合
                case "select_kind":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl444mModel();    //コントロールマスタの呼び出し
                    $this->callView("knjl444mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl444mForm1");
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
$knjl444mCtl = new knjl444mController();
