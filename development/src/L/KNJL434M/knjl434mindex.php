<?php

require_once('for_php7.php');

require_once('knjl434mModel.inc');
require_once('knjl434mQuery.inc');

class knjl434mController extends Controller
{
    public $ModelClassName = "knjl434mModel";
    public $ProgramID      = "KNJL434M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl434m":    //メニュー画面もしくはSUBMITした場合
                case "select_kind":
                    $sessionInstance->setAccessLogDetail("S", $this->ProgramID);
                    $sessionInstance->knjl434mModel();    //コントロールマスタの呼び出し
                    $this->callView("knjl434mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl434mForm1");
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
$knjl434mCtl = new knjl434mController();
//var_dump($_REQUEST);
