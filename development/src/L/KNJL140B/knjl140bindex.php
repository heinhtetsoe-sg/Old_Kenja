<?php

require_once('for_php7.php');

require_once('knjl140bModel.inc');
require_once('knjl140bQuery.inc');

class knjl140bController extends Controller
{
    public $ModelClassName = "knjl140bModel";
    public $ProgramID      = "KNJL140B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl140bForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl140bForm1");
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
$knjl140bCtl = new knjl140bController();
