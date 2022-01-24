<?php

require_once('for_php7.php');

require_once('knjl073yModel.inc');
require_once('knjl073yQuery.inc');

class knjl073yController extends Controller
{
    public $ModelClassName = "knjl073yModel";
    public $ProgramID      = "KNJL073Y";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl073yForm1");
                    }
                    break 2;
                case "":
                case "knjl073y":
                case "change":
                    $sessionInstance->knjl073yModel();
                    $this->callView("knjl073yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl073yCtl = new knjl073yController();
