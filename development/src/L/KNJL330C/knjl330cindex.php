<?php

require_once('for_php7.php');

require_once('knjl330cModel.inc');
require_once('knjl330cQuery.inc');

class knjl330cController extends Controller
{
    public $ModelClassName = "knjl330cModel";
    public $ProgramID      = "KNJL330C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl330c":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl330cModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl330cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl330cForm1");
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
$knjl330cCtl = new knjl330cController();
