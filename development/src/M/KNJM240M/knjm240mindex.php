<?php

require_once('for_php7.php');

require_once('knjm240mModel.inc');
require_once('knjm240mQuery.inc');

class knjm240mController extends Controller
{
    public $ModelClassName = "knjm240mModel";
    public $ProgramID      = "KNJM240M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("knjm240mForm1");
                    break 2;
                case "edit":
                case "reset":
                    $this->callView("knjm240mForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "downloadHead":
                case "downloadCsv":
                case "downloadError":
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjm240mdForm2");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "uploadCsv":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjm240mindex.php?cmd=list";
                    $args["right_src"] = "knjm240mindex.php?cmd=edit&rightInit=1";
                    $args["cols"] = "60%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm240mCtl = new knjm240mController();
