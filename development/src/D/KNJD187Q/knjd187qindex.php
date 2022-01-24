<?php
require_once('knjd187qModel.inc');
require_once('knjd187qQuery.inc');

class knjd187qController extends Controller
{
    public $ModelClassName = "knjd187qModel";
    public $ProgramID      = "KNJD187Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd187q":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd187qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187qCtl = new knjd187qController;
//var_dump($_REQUEST);
