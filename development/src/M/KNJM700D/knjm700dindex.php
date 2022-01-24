<?php

require_once('for_php7.php');

require_once('knjm700dModel.inc');
require_once('knjm700dQuery.inc');

class knjm700dController extends Controller {
    var $ModelClassName = "knjm700dModel";
    var $ProgramID      = "KNJM700D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjm700dForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["use_prg_schoolkind"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/M/KNJM700D/knjm700dindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    } elseif ($sessionInstance->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/M/KNJM700D/knjm700dindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&SCHOOL_KIND=".SCHOOLKIND;
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/M/KNJM700D/knjm700dindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    }
                    $args["right_src"] = "knjm700dindex.php?cmd=edit";
                    $args["cols"] = "22%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm700dCtl = new knjm700dController;
?>
