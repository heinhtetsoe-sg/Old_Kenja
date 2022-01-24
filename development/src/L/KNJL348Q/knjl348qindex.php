<?php

require_once('for_php7.php');

require_once('knjl348qModel.inc');
require_once('knjl348qQuery.inc');

class knjl348qController extends Controller
{
    public $ModelClassName = "knjl348qModel";
    public $ProgramID      = "KNJL348Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl348qForm1");
                    }
                    break 2;
                case "":
                case "knjl348q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl348qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl348qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl348qCtl = new knjl348qController();
