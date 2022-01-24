<?php

require_once('for_php7.php');

require_once('knjl140mModel.inc');
require_once('knjl140mQuery.inc');

class knjl140mController extends Controller
{
    public $ModelClassName = "knjl140mModel";
    public $ProgramID      = "KNJL140M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl140mForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl140mForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl140mCtl = new knjl140mController();
